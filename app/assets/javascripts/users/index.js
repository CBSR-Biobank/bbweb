/**
 * User configuration module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import CommonModule from '../common'
import angular      from 'angular'

const ngModule = angular.module('biobank.users', [ CommonModule ])

const context = require.context('./', true, /^(.(?!index|Spec))*\.js$/)

context.keys().forEach(key => {
  context(key).default(ngModule)
})

export default ngModule.name
