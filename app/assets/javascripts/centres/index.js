/**
 * Centres module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const loadModules = require.context('./modules', true, /[\\\/]index\.js$/)
const moduleNames = []

loadModules.keys().forEach((key) => {
  moduleNames.push(loadModules(key).default)
})

const ngModule = angular.module('biobank.centres', moduleNames)

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

ngModule
  .constant('SHIPMENT_SEND_PROGRESS_ITEMS', [
    'Shipping information',
    'Items to ship',
    'Packed'
  ])
  .constant('SHIPMENT_RECEIVE_PROGRESS_ITEMS', [
    'Sent',
    'Received',
    'Unpacked',
    'Completed'
  ])

export default ngModule.name
