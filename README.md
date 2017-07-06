# Biosamples Validator
[![Build Status](https://travis-ci.org/EMBL-EBI-SUBS/biosamples-validator.svg?branch=master)](https://travis-ci.org/EMBL-EBI-SUBS/biosamples-validator)

Biosamples validator for the submissions validation service.

This validator evaluates the presence of the following  fields in a [Sample](https://github.com/EMBL-EBI-SUBS/subs-data-model/blob/master/src/main/java/uk/ac/ebi/subs/data/submittable/Sample.java):
* `Alias`
* `Release date`
* `Update date`

When one or more Sample relationships exist each must have:
* a `Nature` (_child of, derived from, ..._)
* an `Accession` for the sample target of the relationship 