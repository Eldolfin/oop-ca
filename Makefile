default: run

build:
	gradle build

clean:
	gradle clean

run:
	gradle run

.PHONY: build clean run
