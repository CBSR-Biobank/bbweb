/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule   from '../common'
import angular        from 'angular'
import angularCookies from 'angular-cookies'

const ngModule = angular.module('biobank.users', [
  angularCookies,
  CommonModule
])

const context = require.context('./', true, /^(.(?!index|Spec))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
