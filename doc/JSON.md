JSON 主要用于提供模板中的数据，所有数据在`data`节点中：

```json
{
    "data" : {
        "key" : "value",
        "key2" : "value"
    }
}
```

其它节点可以随意增加和删除，但是下面几个节点有特定的用途：

```json
{
    "data" : { ... },
    "title" : "HTML 页面标题"
}
```

1. title: 这个节点在生成 HTML 页面时会用作页面的标题；
