.PHONY: clean test package

apex: clean
	mvn package -DskipTests -Dmaven.javadoc.skip=true -Papex

package: clean
	mvn package -DskipTests -Dmaven.javadoc.skip=true

clean:
	mvn clean

test:
	mvn package

javadoc:
	mvn compile javadoc:aggregate

docson:
	npm install node
	npm install jquery
	npm install node-docson

docs: docson package
	java -cp "cli/target/*" com.nextdoor.bender.CreateSchema --out-file docs/schema.json --docson
	cd docs && node gendocs.js
