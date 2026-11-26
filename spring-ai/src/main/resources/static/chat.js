import { readAnswerStream } from './stream.js';

const $ = (id) => document.getElementById(id);
const username = $('username');
const input = $('message');
const send = $('send');
const newChat = $('new-chat');
const conversation = $('conversation');
let sessionId = createId();
let activeRequest = null;
let resetting = false;
let conversationStarted = false;

function createId() {
  if (globalThis.crypto.randomUUID) return crypto.randomUUID();
  // getRandomValues also works on HTTP origins without randomUUID support.
  const bytes = crypto.getRandomValues(new Uint8Array(16));
  bytes[6] = (bytes[6] & 15) | 64;
  bytes[8] = (bytes[8] & 63) | 128;
  const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

function notice(text = '') {
  $('notice').textContent = text;
  $('notice').hidden = !text;
}

function updateControls() {
  const responding = Boolean(activeRequest);
  send.disabled = resetting || (!responding && (!username.value.trim() || !input.value.trim()));
  $('send-label').textContent = responding ? '중지' : '전송';
  $('send-icon').textContent = responding ? '■' : '↑';
  // Stop must remain usable even though the message field has been cleared.
  send.type = responding ? 'button' : 'submit';
  input.disabled = resetting || responding;
  input.required = !responding;
  username.disabled = resetting || conversationStarted;
  newChat.disabled = resetting;
  $('status').textContent = resetting ? '이전 대화를 삭제하고 있어요…'
    : responding ? '답변을 작성하고 있어요…'
    : username.value.trim() ? '상담할 준비가 되었어요.' : '사용자 이름을 입력해 주세요.';
}

function scrollDown(force = false) {
  if (force || conversation.scrollHeight - conversation.scrollTop - conversation.clientHeight < 140) {
    conversation.scrollTop = conversation.scrollHeight;
  }
}

function addMessage(role, author, text = '') {
  $('welcome').hidden = true;
  const row = document.createElement('article');
  row.className = `message ${role}`;
  const label = document.createElement('span');
  label.className = 'message-author';
  label.textContent = author;
  const bubble = document.createElement('div');
  bubble.className = 'bubble';
  bubble.textContent = text;
  row.append(label, bubble);
  $('messages').append(row);
  scrollDown(true);
  return { row, bubble };
}

function stopResponse() {
  activeRequest?.controller.abort();
}

async function submitMessage(event) {
  event.preventDefault();
  if (activeRequest) { stopResponse(); return; }
  if (resetting) return;
  const name = username.value.trim();
  const message = input.value;
  if (!name || !message.trim()) { updateControls(); return; }
  notice();
  username.value = name;
  conversationStarted = true;
  addMessage('user', name, message);
  input.value = '';
  const answer = addMessage('assistant', '캠퍼스 AI');
  const request = { controller: new AbortController() };
  activeRequest = request;
  updateControls();

  try {
    const params = new URLSearchParams({ sessionId, username: name });
    const response = await fetch(`/api/chat-stream/chats?${params}`, {
      method: 'POST',
      headers: { 'Content-Type': 'text/plain; charset=UTF-8', Accept: 'text/event-stream' },
      body: message,
      signal: request.controller.signal,
    });
    if (!response.ok) throw new Error(`답변 요청에 실패했습니다 (HTTP ${response.status}). 다시 질문해 주세요.`);
    if (!response.headers.get('content-type')?.toLowerCase().includes('text/event-stream') || !response.body) {
      throw new Error('스트리밍 응답을 받을 수 없습니다. 서버 상태를 확인해 주세요.');
    }
    await readAnswerStream(response.body, (text) => {
      if (request.controller.signal.aborted) return;
      const nearBottom = conversation.scrollHeight - conversation.scrollTop - conversation.clientHeight < 140;
      answer.bubble.append(document.createTextNode(text));
      if (nearBottom) scrollDown(true);
    });
    if (request.controller.signal.aborted) throw new DOMException('Aborted', 'AbortError');
  } catch (error) {
    const stopped = request.controller.signal.aborted;
    const note = document.createElement('span');
    note.className = 'message-note';
    note.textContent = stopped ? '답변 수신을 중지했어요.' : '답변 수신에 실패했어요.';
    if (!answer.bubble.textContent) answer.bubble.remove();
    answer.row.append(note);
    if (!stopped) notice(error instanceof TypeError ? '서버에 연결할 수 없습니다. 연결 상태를 확인한 뒤 다시 질문해 주세요.'
      : error instanceof SyntaxError ? '답변 형식을 확인할 수 없습니다. 다시 질문해 주세요.' : error.message);
  } finally {
    if (activeRequest === request) activeRequest = null;
    updateControls();
    if (!resetting) input.focus();
  }
}

async function resetConversation() {
  if (resetting) return;
  resetting = true;
  notice();
  stopResponse();
  updateControls();
  try {
    const params = new URLSearchParams({ sessionId });
    const response = await fetch(`/api/session/chats?${params}`, { method: 'DELETE' });
    if (!response.ok) throw new Error(`대화 삭제에 실패했습니다 (HTTP ${response.status}). 새 대화를 다시 눌러 주세요.`);
    sessionId = createId();
    $('messages').replaceChildren();
    $('welcome').hidden = false;
    input.value = '';
    conversationStarted = false;
  } catch (error) {
    notice(error instanceof TypeError ? '대화를 삭제하지 못했습니다. 연결 상태를 확인하고 새 대화를 다시 눌러 주세요.' : error.message);
  } finally {
    resetting = false;
    updateControls();
    (conversationStarted || username.value.trim() ? input : username).focus();
  }
}

$('chat-form').addEventListener('submit', submitMessage);
send.addEventListener('click', () => { if (activeRequest) stopResponse(); });
newChat.addEventListener('click', resetConversation);
username.addEventListener('input', updateControls);
input.addEventListener('input', updateControls);
input.addEventListener('keydown', (event) => {
  if (event.key === 'Enter' && !event.shiftKey && !event.isComposing && event.keyCode !== 229) {
    event.preventDefault();
    if (!activeRequest && !resetting) $('chat-form').requestSubmit();
  }
});
updateControls();
