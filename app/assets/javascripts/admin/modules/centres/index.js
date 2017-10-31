/**
 * Centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import CentresModule from '../../../centres';
import StudiesModule from '../../../studies';
import UsersModule from '../../../users';
import angular from 'angular';

const ngModule = angular.module('biobank.admin.centres', [
  CentresModule,
  StudiesModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
