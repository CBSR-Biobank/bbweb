/**
 * Admin common module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule from '../../../common'
import DomainModule from '../../../domain'
import UsersModule  from '../../../users'
import angular      from 'angular';
import uiBootstrap     from 'angular-ui-bootstrap'

const ngModule = angular.module('biobank.admin.common', [
  uiBootstrap,
  CommonModule,
  DomainModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
