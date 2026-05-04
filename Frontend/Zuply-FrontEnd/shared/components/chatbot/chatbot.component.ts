import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ChatbotService, ChatTurn } from '../../../core/services/chatbot.service';

interface Message {
  role: 'user' | 'bot';
  text: string;
  time: string;
  typing?: boolean;  // true while waiting for bot reply
}

const QUICK_CHIPS = [
  'How do I shop here?',
  'Do I need an account?',
  'How does checkout work?',
  'How can I sell on Zuply?',
  'Tell me about AI listing',
];

const WELCOME: Message = {
  role: 'bot',
  text: "👋 Hi! I'm **Zuply AI** — your local shopping assistant.\n\nYou can browse all products without signing up. Login is only needed when you're ready to checkout. How can I help you today?",
  time: now()
};

function now(): string {
  return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('msgList') msgListRef!: ElementRef<HTMLDivElement>;
  @ViewChild('textInput') inputRef!: ElementRef<HTMLTextAreaElement>;

  isOpen       = false;
  hasOpened    = false;   // tracks if user ever opened chat (shows unread badge otherwise)
  unreadCount  = 1;       // welcome message counts as unread until chat is opened
  isTyping     = false;
  userInput    = '';
  messages: Message[] = [];
  chips        = QUICK_CHIPS;
  showChips    = true;

  private history: ChatTurn[] = [];
  private shouldScroll = false;
  private pulseTimer: ReturnType<typeof setInterval> | null = null;

  constructor(private chatbot: ChatbotService) {}

  ngOnInit(): void {
    this.messages = [{ ...WELCOME }];
    // Subtle pulse on FAB to attract attention
    this.pulseTimer = setInterval(() => {}, 5000);
  }

  ngOnDestroy(): void {
    if (this.pulseTimer) clearInterval(this.pulseTimer);
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  toggleChat(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.hasOpened  = true;
      this.unreadCount = 0;
      setTimeout(() => this.inputRef?.nativeElement?.focus(), 150);
      this.shouldScroll = true;
    }
  }

  closeChat(): void { this.isOpen = false; }

  sendChip(chip: string): void {
    this.showChips = false;
    this.userInput = chip;
    this.send();
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  send(): void {
    const text = this.userInput.trim();
    if (!text || this.isTyping) return;

    this.showChips  = false;
    this.userInput  = '';
    this.isTyping   = true;

    // Add user message
    this.messages.push({ role: 'user', text, time: now() });
    this.shouldScroll = true;

    // Add typing placeholder
    this.messages.push({ role: 'bot', text: '', time: now(), typing: true });
    this.shouldScroll = true;

    this.chatbot.send(text, [...this.history]).subscribe({
      next: res => {
        const reply = res.success ? res.data.reply : "Sorry, I'm having trouble right now. Please try again.";
        this.replacePlaceholder(reply);
        // Add to history for multi-turn context
        this.history.push({ role: 'user', text });
        this.history.push({ role: 'model', text: reply });
        // Keep history at max 10 turns (20 entries) to avoid bloat
        if (this.history.length > 20) this.history = this.history.slice(-20);
        this.isTyping     = false;
        this.shouldScroll = true;
        if (!this.isOpen) this.unreadCount++;
      },
      error: (err: HttpErrorResponse) => {
        console.error('[Zuply Chat] HTTP error:', err.status, err.error);
        let msg = "⚠️ Oops! Couldn't reach the server. Check your connection and try again.";
        if (err.status === 0) {
          msg = "⚠️ Cannot connect to server. Make sure the backend is running on port 9090.";
        } else if (err.status === 500) {
          const serverMsg = err.error?.message || '';
          msg = serverMsg
            ? `⚠️ Server error: ${serverMsg}`
            : "⚠️ The AI service encountered an error. Please try again.";
        } else if (err.status === 403) {
          msg = "⚠️ Access denied. Please refresh the page and try again.";
        }
        this.replacePlaceholder(msg);
        this.isTyping     = false;
        this.shouldScroll = true;
      }
    });
  }

  clearChat(): void {
    this.messages  = [{ ...WELCOME }];
    this.history   = [];
    this.showChips = true;
  }

  formatText(text: string): string {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/`(.*?)`/g, '<code>$1</code>')
      .replace(/\n/g, '<br>');
  }

  private replacePlaceholder(reply: string): void {
    for (let i = this.messages.length - 1; i >= 0; i--) {
      if (this.messages[i].typing) {
        this.messages[i] = { role: 'bot', text: reply, time: now() };
        break;
      }
    }
  }

  private scrollToBottom(): void {
    const el = this.msgListRef?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }
}
