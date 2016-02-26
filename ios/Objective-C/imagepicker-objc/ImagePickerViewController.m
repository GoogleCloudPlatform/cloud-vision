// Copyright 2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#import "ImagePickerViewController.h"

@interface ImagePickerViewController ()

@end

@implementation ImagePickerViewController
- (IBAction)loadImageButtonTapped:(UIButton *)sender {
    UIImagePickerController *imagePicker = [[UIImagePickerController alloc] init];
    imagePicker.delegate = self;
    imagePicker.allowsEditing = false;
    imagePicker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    
    [self presentViewController:imagePicker animated:true completion:NULL];
    
}

- (void)imagePickerController:(UIImagePickerController *)imagePicker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info {
    UIImage *pickedImage = info[UIImagePickerControllerOriginalImage];
    self.imageView.contentMode = UIViewContentModeScaleAspectFit;
    self.faceResults.hidden = true;
    self.labelResults.hidden = true;
    [self.spinner startAnimating];
    
    // Base64 encode the image and create the request
    NSString *binaryImageData = [self base64EncodeImage:pickedImage];
    [self createRequest:binaryImageData];
    [imagePicker dismissViewControllerAnimated:true completion:NULL];
}

- (UIImage *) resizeImage: (UIImage*) image toSize: (CGSize)newSize {
    UIGraphicsBeginImageContext(newSize);
    [image drawInRect:CGRectMake(0, 0, newSize.width, newSize.height)];
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}

- (NSString *) base64EncodeImage: (UIImage*)image {
    NSData *imagedata = UIImagePNGRepresentation(image);
    
    // Resize the image if it exceeds the 2MB API limit
    if ([imagedata length] > 2097152) {
        CGSize oldSize = [image size];
        CGSize newSize = CGSizeMake(800, oldSize.height / oldSize.width * 800);
        image = [self resizeImage: image toSize: newSize];
        imagedata = UIImagePNGRepresentation(image);
    }
    
    NSString *base64String = [imagedata base64EncodedStringWithOptions:NSDataBase64EncodingEndLineWithCarriageReturn];
    return base64String;
}

- (void) createRequest: (NSString*)imageData {
    // Create our request URL

    NSString *urlString = @"https://vision.googleapis.com/v1/images:annotate?key=";
    NSString *API_KEY = @"YOUR_API_KEY";
    
    NSString *requestString = [NSString stringWithFormat:@"%@%@", urlString, API_KEY];
    
    NSURL *url = [NSURL URLWithString: requestString];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url];
    [request setHTTPMethod: @"POST"];
    [request addValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request
     addValue:[[NSBundle mainBundle] bundleIdentifier]
     forHTTPHeaderField:@"X-Ios-Bundle-Identifier"];
    
    // Build our API request
    NSDictionary *paramsDictionary =
        @{@"requests":@[
            @{@"image":
                @{@"content":imageData},
             @"features":@[
                @{@"type":@"LABEL_DETECTION",
                  @"maxResults":@10},
                @{@"type":@"FACE_DETECTION",
                  @"maxResults":@10}]}]};
    
    NSError *error;
    NSData *requestData = [NSJSONSerialization dataWithJSONObject:paramsDictionary options:0 error:&error];
    [request setHTTPBody: requestData];

    // Run the request on a background thread
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self runRequestOnBackgroundThread: request];
    });
}

- (void)runRequestOnBackgroundThread: (NSMutableURLRequest*) request {
    NSURLSessionDataTask *task = [[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:^ (NSData *data, NSURLResponse *response, NSError *error) {
        [self analyzeResults:data];
    }];
    [task resume];
}

- (void)analyzeResults: (NSData*)dataToParse {
    
    // Update UI on the main thread
    dispatch_async(dispatch_get_main_queue(), ^{
        
        NSError *e = nil;
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:dataToParse options:kNilOptions error:&e];
        
        NSArray *responses = [json objectForKey:@"responses"];
        NSLog(@"%@", responses);
        NSDictionary *responseData = [responses objectAtIndex: 0];
        NSDictionary *errorObj = [json objectForKey:@"error"];
        
        [self.spinner stopAnimating];
        self.imageView.hidden = true;
        self.labelResults.hidden = false;
        self.faceResults.hidden = false;
        
        // Check for errors
        if (errorObj) {
            NSString *errorString1 = @"Error code ";
            NSString *errorCode = [errorObj[@"code"] stringValue];
            NSString *errorString2 = @": ";
            NSString *errorMsg = errorObj[@"message"];
            self.labelResults.text = [NSString stringWithFormat:@"%@%@%@%@", errorString1, errorCode, errorString2, errorMsg];
        } else {
            // Get face annotations
            NSDictionary *faceAnnotations = [responseData objectForKey:@"faceAnnotations"];
            if (faceAnnotations != NULL) {
                // Get number of faces detected
                NSInteger numPeopleDetected = [faceAnnotations count];
                NSString *peopleStr = [NSString stringWithFormat:@"%lu", (unsigned long)numPeopleDetected];
                NSString *faceStr1 = @"People detected: ";
                NSString *faceStr2 = @"\n\nEmotions detected:\n";
                self.faceResults.text = [NSString stringWithFormat:@"%@%@%@", faceStr1, peopleStr, faceStr2];
                
                NSArray *emotions = @[@"joy", @"sorrow", @"surprise", @"anger"];
                NSMutableDictionary *emotionTotals = [NSMutableDictionary dictionaryWithObjects:@[@0.0,@0.0,@0.0,@0.0]forKeys:@[@"sorrow",@"joy",@"surprise",@"anger"]];
                NSDictionary *emotionLikelihoods = @{@"VERY_LIKELY": @0.9, @"LIKELY": @0.75, @"POSSIBLE": @0.5, @"UNLIKELY": @0.25, @"VERY_UNLIKELY": @0.0};
                
                // Sum all detected emotions
                for (NSDictionary *personData in faceAnnotations) {
                    for (NSString *emotion in emotions) {
                        NSString *lookup = [emotion stringByAppendingString:@"Likelihood"];
                        NSString *result = [personData objectForKey:lookup];
                        double newValue = [emotionLikelihoods[result] doubleValue] + [emotionTotals[emotion] doubleValue];
                        NSNumber *tempNumber = [[NSNumber alloc] initWithDouble:newValue];
                        [emotionTotals setValue:tempNumber forKey:emotion];
                    }
                }
                
                // Get emotion likelihood as a % and display it in the UI
                for (NSString *emotion in emotionTotals) {
                    double emotionSum = [emotionTotals[emotion] doubleValue];
                    double totalPeople = [faceAnnotations count];
                    double likelihoodPercent = emotionSum / totalPeople;
                    NSString *percentString = [[NSString alloc] initWithFormat:@"%2.0f%%",(likelihoodPercent*100)];
                    NSString *emotionPercentString = [NSString stringWithFormat:@"%@%@%@%@", emotion, @": ", percentString, @"\r\n"];
                    self.faceResults.text = [self.faceResults.text stringByAppendingString:emotionPercentString];
                }
            } else {
                self.faceResults.text = @"No faces found";
            }
            
            // Get label annotations
            NSDictionary *labelAnnotations = [responseData objectForKey:@"labelAnnotations"];
            NSInteger numLabels = [labelAnnotations count];
            NSMutableArray *labels = [[NSMutableArray alloc] init];
            if (numLabels > 0) {
                NSString *labelResultsText = @"Labels found: ";
                for (NSDictionary *label in labelAnnotations) {
                    NSString *labelString = [label objectForKey:@"description"];
                    [labels addObject:labelString];
                }
                for (NSString *label in labels) {
                    // if it's not the last item add a comma
                    if (labels[labels.count - 1] != label) {
                        NSString *commaString = [label stringByAppendingString:@", "];
                        labelResultsText = [labelResultsText stringByAppendingString:commaString];
                    } else {
                        labelResultsText = [labelResultsText stringByAppendingString:label];
                    }
                }
                self.labelResults.text = labelResultsText;
            } else {
                self.labelResults.text = @"No labels found";
            }
        }
    });
    
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)imagePicker {
    [imagePicker dismissViewControllerAnimated:true completion:NULL];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    self.faceResults.hidden = true;
    self.labelResults.hidden = true;
    self.spinner.hidesWhenStopped = true;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
