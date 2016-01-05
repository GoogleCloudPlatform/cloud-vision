Java image labelling Vision API example. See [tutorial](https://cloud.google.com/vision/docs/label-tutorial).

This sample requires the `google-api-services-vision` Maven package be installed
locally.

    cd ../google-api-services-vision
    mvn install

To build and run the sample:

    mvn clean compile assembly:single
    java -cp target/label-1.0-SNAPSHOT-jar-with-dependencies.jar com.google.cloud.vision.samples.label.LabelApp ../../data/label/cat.jpg
