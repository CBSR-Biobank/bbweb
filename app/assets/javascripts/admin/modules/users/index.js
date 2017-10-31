/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule    from '../../../common'
import StudiesModule   from '../../../studies'
import UsersModule     from '../../../users'
import angular         from 'angular'
import angularGettext  from 'angular-gettext'
import angularUiRouter from '@uirouter/angularjs'

const ngModule = angular.module('biobank.admin.users', [
  angularGettext,
  angularUiRouter,
  CommonModule,
  StudiesModule,
  UsersModule
])
const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
