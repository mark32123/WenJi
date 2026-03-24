/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        tianqing: {
          DEFAULT: '#A8DADC',
          50: '#F0F7F7',
          100: '#E0EFEF',
          200: '#C1DFE0',
          300: '#A8DADC',
          400: '#89CBCD',
          500: '#6AB9BD',
          600: '#4BA7AC',
          700: '#3A8A8F',
          800: '#2D6D71',
          900: '#1F5053',
        },
        yuebai: {
          DEFAULT: '#F0F4F8',
          50: '#FFFFFF',
          100: '#F7F9FB',
          200: '#F0F4F8',
          300: '#DCE4ED',
          400: '#C8D4E2',
          500: '#B4C4D7',
          600: '#9AB4CC',
          700: '#80A4C1',
          800: '#6694B6',
          900: '#4C84AB',
        },
        dailan: {
          DEFAULT: '#2D4059',
          50: '#E8EBEF',
          100: '#D1D7DF',
          200: '#A3AFBF',
          300: '#75879F',
          400: '#475F7F',
          500: '#2D4059',
          600: '#243347',
          700: '#1B2635',
          800: '#121923',
          900: '#090D11',
        },
        zhusha: {
          DEFAULT: '#C0392B',
          50: '#F9E6E4',
          100: '#F3CDC9',
          200: '#E79B93',
          300: '#DB695D',
          400: '#CF3727',
          500: '#C0392B',
          600: '#9A2E23',
          700: '#74231A',
          800: '#4E1812',
          900: '#280D09',
        },
        mohei: {
          DEFAULT: '#1A1A1A',
          50: '#F2F2F2',
          100: '#E6E6E6',
          200: '#CCCCCC',
          300: '#B3B3B3',
          400: '#999999',
          500: '#808080',
          600: '#666666',
          700: '#4D4D4D',
          800: '#333333',
          900: '#1A1A1A',
        },
        xuanzhi: {
          DEFAULT: '#F5F2EB',
          50: '#FDFCF9',
          100: '#FAF8F3',
          200: '#F5F2EB',
          300: '#EDE8DD',
          400: '#E5DECF',
          500: '#DDD4C1',
          600: '#C9BEA5',
          700: '#B5A889',
          800: '#A1926D',
          900: '#8D7C51',
        },
        qinghui: {
          DEFAULT: '#8B9A9C',
          50: '#F2F4F4',
          100: '#E5E9EA',
          200: '#CCD3D4',
          300: '#B3BDBE',
          400: '#9AA7A8',
          500: '#8B9A9C',
          600: '#6E7B7C',
          700: '#515C5D',
          800: '#343D3E',
          900: '#171E1F',
        },
      },
      fontFamily: {
        serif: [
          'STSong',
          'SimSun',
          'Songti SC',
          'Noto Serif SC',
          'Source Han Serif SC',
          'serif',
        ],
        sans: [
          'PingFang SC',
          'Microsoft YaHei',
          'Hiragino Sans GB',
          'Noto Sans SC',
          'Source Han Sans SC',
          'sans-serif',
        ],
        display: [
          'STKaiti',
          'Kaiti SC',
          'Noto Serif SC',
          'serif',
        ],
      },
      fontSize: {
        'display-xl': ['3rem', { lineHeight: '1.2', letterSpacing: '0.05em' }],
        'display-lg': ['2.25rem', { lineHeight: '1.3', letterSpacing: '0.04em' }],
        'display-md': ['1.75rem', { lineHeight: '1.4', letterSpacing: '0.03em' }],
        'display-sm': ['1.25rem', { lineHeight: '1.5', letterSpacing: '0.02em' }],
      },
      backgroundImage: {
        'paper-texture': "url(\"data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E\")",
        'ink-wash': "linear-gradient(180deg, rgba(245,242,235,0) 0%, rgba(245,242,235,0.3) 50%, rgba(245,242,235,0.6) 100%)",
      },
      boxShadow: {
        'paper': '0 2px 8px rgba(45, 64, 89, 0.08), 0 1px 2px rgba(45, 64, 89, 0.04)',
        'paper-hover': '0 4px 16px rgba(45, 64, 89, 0.12), 0 2px 4px rgba(45, 64, 89, 0.06)',
        'seal': '0 2px 4px rgba(192, 57, 43, 0.3)',
        'ink': '0 0 20px rgba(45, 64, 89, 0.15)',
      },
      animation: {
        'seal-stamp': 'seal-stamp 0.4s ease-out forwards',
        'ink-spread': 'ink-spread 0.6s ease-out forwards',
        'fade-in-up': 'fade-in-up 0.5s ease-out forwards',
        'slide-in-right': 'slide-in-right 0.4s ease-out forwards',
        'float': 'float 3s ease-in-out infinite',
        'pulse-soft': 'pulse-soft 2s ease-in-out infinite',
      },
      keyframes: {
        'seal-stamp': {
          '0%': { 
            transform: 'scale(1.5) rotate(-15deg)', 
            opacity: '0' 
          },
          '50%': { 
            transform: 'scale(0.95) rotate(0deg)', 
            opacity: '1' 
          },
          '100%': { 
            transform: 'scale(1) rotate(0deg)', 
            opacity: '1' 
          },
        },
        'ink-spread': {
          '0%': { 
            transform: 'scale(0)', 
            opacity: '0.8' 
          },
          '100%': { 
            transform: 'scale(1)', 
            opacity: '0' 
          },
        },
        'fade-in-up': {
          '0%': { 
            transform: 'translateY(10px)', 
            opacity: '0' 
          },
          '100%': { 
            transform: 'translateY(0)', 
            opacity: '1' 
          },
        },
        'slide-in-right': {
          '0%': { 
            transform: 'translateX(-20px)', 
            opacity: '0' 
          },
          '100%': { 
            transform: 'translateX(0)', 
            opacity: '1' 
          },
        },
        'float': {
          '0%, 100%': { 
            transform: 'translateY(0)' 
          },
          '50%': { 
            transform: 'translateY(-5px)' 
          },
        },
        'pulse-soft': {
          '0%, 100%': { 
            opacity: '1' 
          },
          '50%': { 
            opacity: '0.7' 
          },
        },
      },
      transitionDuration: {
        '400': '400ms',
        '600': '600ms',
        '800': '800ms',
      },
      transitionTimingFunction: {
        'elegant': 'cubic-bezier(0.4, 0, 0.2, 1)',
      },
      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
      },
      borderRadius: {
        'seal': '4px',
      },
    },
  },
  plugins: [],
}
