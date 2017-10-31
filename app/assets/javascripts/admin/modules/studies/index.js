/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import AdminCommonModule from '../common'
import BaseModule        from '../../../base'
import DomainModule      from '../../../domain'
import StudiesModule     from '../../../studies'
import UsersModule       from '../../../users'
import angular           from 'angular';

const ngModule = angular.module('biobank.admin.studies', [
  AdminCommonModule,
  BaseModule,
  DomainModule,
  StudiesModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
