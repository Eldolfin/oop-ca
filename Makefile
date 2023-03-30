default: run

build:
	gradle build

clean:
	gradle clean

run:
	# gradle -q --console plain run # slow af
	gradle jar
	java -jar app/build/libs/app.jar

.PHONY: build clean run
