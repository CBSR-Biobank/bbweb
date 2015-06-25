## Installing Java ##

Oracle's Java SE is not included in the official Linux repositories due to license problems created by
Oracle. However, it can still be installed on Linux.

1. Download Sun/Oracle Java JDK from here (current version is JDK 8 Update 45) :
    ```
    http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
    ```

    and move the file to the `/usr/lib/jvm` directory.

    Note: Select the appropriate package for your architecture: x86 or x64.

1. Extract the tarball (replace the `*` with the proper text):
    ```
    cd /usr/lib/jvm
    sudo tar zxvf jdk-8u45-linux-*.tag.gz
    ```

    There will be a newly created folder on the same path with the extracted files

1. Create a soft link to have a more generic name:
    ```
    sudo ln -s jdk1.8.0_45 java-8-oracle
    ```

1. Add the new version of java, javac and javaws as an system alternative and give it priority 1
    ```
    sudo update-alternatives --install "/usr/bin/java" "java" "/usr/lib/jvm/java-8-oracle/bin/java" 1
    sudo update-alternatives --install "/usr/bin/javac" "javac" "/usr/lib/jvm/java-8-oracle/bin/javac" 1
    sudo update-alternatives --install "/usr/bin/javaws" "javaws" "/usr/lib/jvm/java-8-oracle/bin/javaws" 1
    ```

1. Select the new alternatives to be used. To select the *java* alternative:
    ```
    sudo update-alternatives --config java
    ```

    To select the *javac* alternative:
    ```
    sudo update-alternatives --config javac
    ```

    To select the *javaws* alternative:
    ```
    sudo update-alternatives --config javaws
    ```

1. Test your newly added *java* and *javac*. The `java -version` command should return:
    ```
    java version "1.8.0_45"
    Java(TM) SE Runtime Environment (build 1.8.0_45-b14)
    Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)
    ```

    and the `javac -version` command should return:
    ```
    javac 1.8.0_45
    ```

1. [Optional] Update system paths. Open `/etc/profile` with you favorite text editor, ie:
    ```
    sudo vi /etc/profile
    ```

    Navigate to the end of the file and add these contents:
    ```
    JAVA_HOME=/usr/lib/jvm/java-8-oracle
    PATH=$PATH:$HOME/bin:$JAVA_HOME/bin
    export JAVA_HOME
    export JAVA_BIN
    export PATH
    ```

    Reload your system wide PATH `/etc/profile` with
    ```
    . /etc/profile
    ```
