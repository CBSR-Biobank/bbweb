/**
 * Domain module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import angular from 'angular';

const ngModule = angular.module('biobank.domain', [])

const context = require.context('./', true, /^(.(?!index\.|Spec\.))*js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
