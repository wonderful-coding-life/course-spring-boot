// SSE frames may span network chunks, including inside UTF-8 characters or CRLF.
export async function readAnswerStream(body, onText) {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let data = [];
  let received = false;

  function dispatch() {
    if (!data.length) return;
    const payload = data.join('\n');
    data = [];
    const text = JSON.parse(payload);
    if (typeof text !== 'string') throw new Error('답변 형식을 확인할 수 없습니다. 다시 시도해 주세요.');
    if (text.length) received = true;
    onText(text);
  }

  function consume(final = false) {
    let match;
    while ((match = /[\r\n]/.exec(buffer))) {
      const index = match.index;
      if (!final && buffer[index] === '\r' && index === buffer.length - 1) break;
      const line = buffer.slice(0, index);
      const length = buffer[index] === '\r' && buffer[index + 1] === '\n' ? 2 : 1;
      buffer = buffer.slice(index + length);
      if (!line) dispatch();
      else if (line.startsWith('data:')) data.push(line.slice(5).replace(/^ /, ''));
    }
    // SSE dispatches only complete events; a partial event at EOF is a failure.
    if (final && (buffer.length || data.length)) {
      throw new Error('답변 수신이 끊겼습니다. 표시된 답변을 확인하고 다시 질문해 주세요.');
    }
  }

  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      consume();
    }
    buffer += decoder.decode();
    consume(true);
    if (!received) throw new Error('답변을 받지 못했습니다. 다시 질문해 주세요.');
  } finally {
    try { await reader.cancel(); } catch { /* The request may already be aborted. */ }
    reader.releaseLock();
  }
}
