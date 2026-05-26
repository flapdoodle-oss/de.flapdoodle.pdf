# Basics

## Hello World
              
create a document factory:

```java
DocumentFactory factory = DocumentFactory.builder()
  .pageSize(PageSize.A4)
  .addBlocks(new Text("hello world!"))
  .build();
```

render to an output stream:

```java
factory.render(out);
```
![hello-world.png](hello-world.png)

[hello-world.pdf](hello-world.pdf)

## Meta Data

Add creation date and producer:

```java
DocumentFactory factory = DocumentFactory.builder()
  .pageSize(PageSize.A4)
  .meta(Meta.empty()
    .withCreationDate(creationDate)
    .withTitle("MetaData Sample")
    .withSubject("this is how we do it")
    .withAuthor("Its me:)")
    .withCreator("flapdoodle test")
    .withProducer("OpenPDF"))
  .addBlocks(new Text("created at "+creationDate))
  .build();
```

![meta-data.png](meta-data.png)

[meta-data.pdf](meta-data.pdf)
