/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import CommonModule from '../../../common'
import DomainModule from '../../../domain'
import UsersModule  from '../../../users'
import angular      from 'angular';
import uiBootstrap     from 'angular-ui-bootstrap'

/**
 * A Webpack module with common Biobank Administration functionality.
 *
 * @memberOf admin.common
 */
const ngAdminCommonModule = angular.module('biobank.admin.common', [
  uiBootstrap,
  CommonModule,
  DomainModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngAdminCommonModule)
})

export default ngAdminCommonModule.name
