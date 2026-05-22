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