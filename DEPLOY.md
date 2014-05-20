Publish Grok Distribution Package
------

Publish Maven artifact
------------

**Publish to snapshot repository**

     mvn -DperformRelease=true deploy


**Publish to release repository**

    mvn -DperformRelease=true release:clean
    mvn -DperformRelease=true release:prepare
    mvn -DperformRelease=true release:perform

Artifact is now in staging repository.
Connect https://oss.sonatype.org/ , select staging repository and click "close" -> "release" will finally release it.
