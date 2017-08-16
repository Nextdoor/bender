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

docson:
	npm install node -g
	npm install jquery -g
	npm install node-docson -g

docs: docson package
	java -cp "cli/target/*" com.nextdoor.bender.CreateSchema --out-file docs/schema.json --docson
	cd docs && node gendocs.js
