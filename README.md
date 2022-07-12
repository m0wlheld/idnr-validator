# idnr-validator

Utility to validate a German tax identification number (<q>steuerliche Identifikationsnummer</q>, _IdNr_) for formal correctness. This concerns both the length, the content and check digit (<q>Prüfziffer</q>).

## No Warranty

The checks are done based on the documented, publicly-available rules (see [References](#references)). The author is not associated to the German government or tax authorities. There is no warranty whatsoever, that the validation outcome is correct.

## IdNr
The tax identification number is an eleven-digit number, the 11th digit being a check digit. According to documentation tax identification number must meet the following criteria:

* no leading zeros allowed
* the 11<sup>th</sup> digit is a check digit and is calculated from digits 1-10 (see below)
* in the first 10 digits of the identification number, exactly one digit must  be duplicated or triplicated
* if there are 3 identical digits in positions 1 to 10, these identical digits must never appear in directly consecutive positions.


## Usage

### Build
This is a Java 11 project, using Apache Maven build utility. After having cloned this repository, perform the following steps to create a usable artifact (Java archive):

```bash
mvnw clean install
```

You'll find the artifact in the `target`-folder. The name matches pattern `idnr-validator-(version).jar`.

### Use

Having integrated the JAR file in your Java application, validating a given IdNr. is done by calling either one of the following methods:

```java
org.dahlen.IdNrValidator.isValidIdNr(String)
```

this will return <code>true</code> or <code>false</code> as a result or 

```java
org.dahlen.IdNrValidator.validateIdNr(String)
```

this will return a Set of validation errors (if any).

### Javadoc

The API documentation (Javadoc) can be generated from command-line using

```bash
mvnw site:site
```

This will output the documentation to sub-folder `target/site`.

## References

 * [Deutsche Rentenversicherung: Prüfziffernberechnung](https://www.zfa.deutsche-rentenversicherung-bund.de/de/Inhalt/public/4_ID/47_Pruefziffernberechnung/001_Pruefziffernberechnung.pdf)
 * [Elster: Prüfung der Steuer- und Steueridentifikationsnummer](https://download.elster.de/download/schnittstellen/Pruefung_der_Steuer_und_Steueridentifikatsnummer.pdf)