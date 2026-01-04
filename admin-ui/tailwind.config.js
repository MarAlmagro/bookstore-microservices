const tokens = require('./src/main/resources/design/tokens.json');

module.exports = {
  content: [
    './src/main/resources/templates/**/*.html',
    './src/main/resources/static/js/**/*.js'
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        'brand-primary': tokens.colors.brand.primary,
        'brand-secondary': tokens.colors.brand.secondary,
        'status-success': tokens.colors.status.success,
        'status-error': tokens.colors.status.error,
        'status-warning': tokens.colors.status.warning,
        'status-info': tokens.colors.status.info,
      },
      fontFamily: {
        sans: tokens.typography.fontFamily.sans.split(', '),
        mono: tokens.typography.fontFamily.mono.split(', ')
      },
      spacing: {
        'xs': tokens.spacing.xs,
        'sm': tokens.spacing.sm,
        'md': tokens.spacing.md,
        'lg': tokens.spacing.lg,
        'xl': tokens.spacing.xl,
        '2xl': tokens.spacing['2xl']
      },
      borderRadius: {
        'sm': tokens.borderRadius.sm,
        'md': tokens.borderRadius.md,
        'lg': tokens.borderRadius.lg,
        'xl': tokens.borderRadius.xl,
        'full': tokens.borderRadius.full
      },
      boxShadow: {
        'sm': tokens.shadows.sm,
        'md': tokens.shadows.md,
        'lg': tokens.shadows.lg,
        'xl': tokens.shadows.xl
      }
    }
  },
  plugins: []
}
