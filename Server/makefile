all:
	javac -d . -cp .:java-json.jar:xmlpull.jar WeatherDataHelper.java
	java -cp .:java-json.jar:mariadb.jar:xmlpull.jar maclab.everywear.WeatherDataHelper
db:
	javac -d . -cp . EverywearDB.java
	java -cp .:mariadb.jar maclab.everywear.EverywearDB
foo:
	javac -d . -cp .:java-json.jar:xmlpull.jar foo.java
	java -cp .:java-json.jar:mariadb.jar:xmlpull.jar foo
clean:
	rm *.tmp
