# 一个简单的 XML 模板文件

提示：下面的示例不再更新，最新模板示例请参考源代码`tests`目录中的`test.xml`。

```xml
<?xml version="1.0" encoding="utf-8"?>

<textpdf>
  <title>示例文档</title>
  <para align="right">合同编号：20150011-610</para>
  <para size="large">甲方：<value name="jiafang" /></para>
  <para size="large">乙方：<value name="yifang" /></para>
  <vspace size="2" />
  <para>根据《中华人民共和国》及相关法律、法规，为明确甲乙双方权利义务，
双方在平等、自愿的基础上，就甲方将某某事项委托给乙方，经协商一致，签订本合同</para>
  <section>第一条</section>
  <para>1、甲方将位于<value name="address" />的企业自有办公室出租给乙方，
建筑面积<value name="size" />平方米，乙方用此建筑<value name="usage" />。</para>
  <para>2、乙方提供<b>营业执照</b>等有效身份证件，验证后<u>复印备份</u>，
所有复印件仅供本次合同使用。</para>

  <section>期限</section>
  <para>无限。</para>

  <para>甲方签字：<space size="20">乙方签字：</para>
  <para>日期：<space size="22">日期：</para>

</textpdf>
```