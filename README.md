# Spring Batch File Sort With Gemfire
This project is intended to be a proof of concept (PoC) on using Apache Geode/Pivotal Gemfire as an in memory distributed data structure to be able to do high performance computations on in a traditional batch environment.

In order to generate the input files, `gensort` is used.  You can find it via the following locations:
* The original distribution (available for Linux or Windows: http://www.ordinal.com/gensort.html
* An updated version for OS X: https://github.com/scslab/bad

For intial test set of data (1GB across 10 files) I used the following commands:
```
gensort -b0 1050000 part0
gensort -b1050000 1050000 part1
gensort -b2100000 1050000 part2
gensort -b3150000 1050000 part3
gensort -b4200000 1050000 part4
gensort -b5250000 1050000 part5
gensort -b6300000 1050000 part6
gensort -b7350000 1050000 part7
gensort -b8400000 1050000 part8
gensort -b9450000 1050000 part9
```

To verify the results, use the following commands:
```
valsort -o out0.sum output0.dat
valsort -o out1.sum output1.dat
valsort -o out2.sum output2.dat
valsort -o out3.sum output3.dat
valsort -o out4.sum output4.dat
valsort -o out5.sum output5.dat
valsort -o out6.sum output6.dat
valsort -o out7.sum output7.dat
valsort -o out8.sum output8.dat
valsort -o out9.sum output9.dat
cat out0.sum out1.sum out2.sum out3.sum out4.sum out5.sum out6.sum out7.sum out8.sum out9.sum > all.sum
valsort -s all.sum 
```

# Starting the nodes locally

Configure the master's application.properties as follows:

```
spring.application.name=batch-gemfire-sort
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.url=<JDBC_URL>
spring.datasource.username=<JDBC_USER>
spring.datasource.password=<JDBC_PASSWORD>
spring.batch.initialize-schema=always
spring.batch.job.enabled=false
amazonProperties.endpointUrl=<ENDPOINT_URL>
amazonProperties.accessKey=<ACCESS_KEY>
amazonProperties.secretKey=<SECRET_KEY>
amazonProperties.bucketName=<BUCKET_NAME>
spring.batch.grid-size=<NUMBER_OF_NODES>
```


Configure the worker's application.properties as follows:

```
spring.application.name=batch-gemfire-sort-worker
spring.datasource.driverClassName=com.mysql.jdbc.Driver
spring.datasource.url=<JDBC_URL>
spring.datasource.username=<JDBC_USER>
spring.datasource.password=<JDBC_PASSWORD>
spring.batch.initialize-schema=never
amazonProperties.endpointUrl=<ENDPOINT_URL>
amazonProperties.accessKey=<ACCESS_KEY>
amazonProperties.secretKey=<SECRET_KEY>
amazonProperties.bucketName=<BUCKET_NAME>
spring.batch.working-directory=<LOCATION_TO_WRITE_INPUT_FILES> # optional if configuring via the command line as noted below
```

To launch the nodes locally once configuring the above:
```
java -Dgemfire.start-locator=localhost[10334] -jar batch-gemfire-file-sort-worker/target/batch-gemfire-file-sort-worker-0.0.1-SNAPSHOT.jar --partition.name=0 --spring.data.gemfire.name=SortServerZero --spring.batch.working-directory=/Users/mminella/tmp/partition0
java -jar batch-gemfire-file-sort-worker/target/batch-gemfire-file-sort-worker-0.0.1-SNAPSHOT.jar --partition.name=1 --spring.data.gemfire.name=SortServerOne --spring.batch.working-directory=/Users/mminella/tmp/partition1
java -jar batch-gemfire-file-sort-worker/target/batch-gemfire-file-sort-worker-0.0.1-SNAPSHOT.jar --partition.name=2 --spring.data.gemfire.name=SortServerTwo --spring.batch.working-directory=/Users/mminella/tmp/partition2
java -jar batch-gemfire-file-sort-worker/target/batch-gemfire-file-sort-worker-0.0.1-SNAPSHOT.jar --partition.name=3 --spring.data.gemfire.name=SortServerThree --spring.batch.working-directory=/Users/mminella/tmp/partition3

java -jar batch-gemfire-file-sort-master/target/batch-gemfire-file-sort-master-0.0.1-SNAPSHOT.jar
```

# TODO
1. Write file from local partition. - DONE
2. Convert to LRPs instead of tasks. - DONE
2. Upload files to S3.
3. Download input files from S3. - DONE
4. Run on PKS
5. Scale up
