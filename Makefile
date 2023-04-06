default: run

generate:
	python app/src/main/python/generate_ast.py

clean:
	gradle clean

jar: generate
	gradle jar

run: jar
	java -jar app/build/libs/app.jar

.PHONY: build clean run jar generate
