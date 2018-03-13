/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
import CommonModule    from '../../../common'
import DomainModule    from '../../../domain'
import angular         from 'angular';
import angularUiRouter from '@uirouter/angularjs';

const ngModule = angular.module(
  'biobank.shipmentsTableModule', [
    CommonModule,
    DomainModule,
    angularUiRouter
  ])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
