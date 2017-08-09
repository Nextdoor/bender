.PHONY: clean test package

apex: clean
	mvn package -DskipTests -Dmaven.javadoc.skip=true -Papex

package: clean
	mvn package -DskipTests -Dmaven.javadoc.skip=true

clean:
	mvn clean

test:
	mvn test

javadoc:
	mvn compile javadoc:aggregate

doca:
	npm install node
	npm install doca -g

docs: doca package
	rm -rf docs
	rm -rf tmp/schema
	rm -rf tmp/doca
	mkdir docs
	mkdir -p tmp/schema
	java -cp "cli/target/cli.jar" com.nextdoor.bender.CreateSchema --out-file tmp/schema/doca.json --doca
	doca init -i tmp/schema -o tmp/doca
	sed -i '.original' 's/Example API Documentation/Bender Configuration Documentation/g' tmp/doca/config.js
	cd tmp/doca && npm install && npm run build:nojs
	cp tmp/doca/build/* docs/
