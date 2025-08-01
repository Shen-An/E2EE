function splitText(locales, text) {
  const segments = Array.from(
    new Intl.Segmenter(locales, { granularity: 'word' }).segment(text)
  );
  
  // 提取词语并过滤非词语内容
  const words = segments
    .filter(seg => seg.isWordLike)
    .map(seg => seg.segment);
  
  console.log(words);
}

// 示例调用
splitText('zh-CN', '老王，今晚码头见，有钻石呢，去拿粉吧。');