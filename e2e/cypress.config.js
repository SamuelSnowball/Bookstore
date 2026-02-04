const { defineConfig } = require('cypress')

module.exports = defineConfig({
  e2e: {
    baseUrl: 'http://localhost:5173',
    chromeWebSecurity: false, // Required for Stripe iframe interactions
    setupNodeEvents(on, config) {
      on('before:browser:launch', (browser, launchOptions) => {
        if (browser.family === 'chromium' && browser.name !== 'electron') {
          launchOptions.args.push('--disable-password-manager-reauthentication')
          launchOptions.args.push('--disable-features=PasswordLeakDetection')
          launchOptions.args.push('--disable-features=PasswordManager')
          launchOptions.args.push('--no-service-autorun')
          launchOptions.preferences.default['credentials_enable_service'] = false
          launchOptions.preferences.default['profile.password_manager_enabled'] = false
        }
        return launchOptions
      })
    },
  },
})
