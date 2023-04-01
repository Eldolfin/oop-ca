default: run

build:
	python app/src/main/python/generate_ast.py
	gradle build

clean:
	gradle clean

jar: build
	gradle jar

run: jar
	java -jar app/build/libs/app.jar

.PHONY: build clean run jar
