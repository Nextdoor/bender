# Misc
PATH ?= /usr/local/bin:${PATH}
APEX ?= /usr/local/bin/apex

# The entire Log Pipeline runs in the US-East-1 region all the time
export AWS_REGION=us-east-1

# Allow the user to override the verbosity -- but lets start by default
# with full debug logging
APEX_OPTS ?= -l debug

# By default, we run dry..
ifneq ($(DRY),false)
	APEX_DRY := --dry-run
endif

# The user can set APEX_ENV to either: production, staging
ENV ?= staging

# Find all of our functions
FUNCTIONS := $(wildcard functions/*)

# Find all function configs. Note can't use wildcard bcause $ must be escaped
# when passing to java via shell.
CONFIGS := ${shell find functions/*/config/*.json -type f | sed 's/\$$/\\$$/g'}

.PHONY: build $(FUNCTIONS)

build: compile test validate

${APEX}:
	curl https://raw.githubusercontent.com/apex/apex/master/install.sh | sh

compile:
	cd ../ && mvn install package -DskipTests -Dmaven.javadoc.skip=true

test: compile
	cd ../ && mvn test

schema: compile
	java -cp "../cli/target/*" com.nextdoor.bender.CreateSchema --out-file schema.json

validate: schema
	java -cp "../cli/target/*" com.nextdoor.bender.ValidateSchema --schema schema.json --configs ${CONFIGS}

deploy: validate
	${APEX} ${APEX_DRY} ${APEX_OPTS} deploy

