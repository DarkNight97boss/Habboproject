/**
 * Custom-element selectors usati dalla CSS ufficiale habbo.it
 * (originariamente component AngularJS). Qui sono solo "hook"
 * per attivare regole CSS scope-ate (es. habbo-header-large
 * .login-form__social { flex-direction: column }).
 */
import type { DetailedHTMLProps, HTMLAttributes } from 'react';

type HabboEl<T = HTMLElement> = DetailedHTMLProps<HTMLAttributes<T> & { type?: 'small' | 'large' }, T>;

declare module 'react' {
    namespace JSX {
        interface IntrinsicElements {
            'habbo-header-large': HabboEl;
            'habbo-register-banner': HabboEl;
            'habbo-navigation': HabboEl;
            'habbo-login-form': HabboEl;
            'habbo-facebook-connect': HabboEl;
            'habbo-google-connect': HabboEl;
            'habbo-apple-connect': HabboEl;
            'habbo-tabs': HabboEl;
            'habbo-tab': HabboEl;
            'habbo-news-list': HabboEl;
            'habbo-news-item': HabboEl;
            'habbo-rpx-login': HabboEl;
            'habbo-claim-password': HabboEl;
            'habbo-web-pages': HabboEl;
            'habbo-footer': HabboEl;
            'habbo-register-banner': HabboEl;
            'habbo-landing-menu': HabboEl;
        }
    }
}
