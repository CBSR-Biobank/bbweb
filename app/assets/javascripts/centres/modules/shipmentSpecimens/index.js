/**
 * Centres configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import DomainModule    from '../../../domain'
import angular         from 'angular';
import angularUiRouter from '@uirouter/angularjs'

const ngModule = angular.module('biobank.shipmentSpecimens', [
  DomainModule,
  angularUiRouter
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
