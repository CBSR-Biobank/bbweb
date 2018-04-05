/**
 * Domain module
 *
 * Provides factories for various client side domain objects.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import BaseModule   from '../base'
import angular      from 'angular';

/**
 * Domain model for data returned by the Biobank server.
 * @namespace domain
 */
const ngDomainModule = angular.module('biobank.domain', [ BaseModule ])

/**
 * Domain entities related to user access management.
 * @namespace domain.access
 */

/**
 * Domain entities related to user access management.
 * @namespace domain.annotations
 */

/**
 * Domain entities related to users.
 * @namespace domain.users
 */

/**
 * Domain entities related to studies.
 * @namespace domain.studies
 */

/**
 * Domain entities related to participants.
 * @namespace domain.participants
 */

/**
 * Domain entities related to centres.
 * @namespace domain.centres
 */

/**
 * Filter classes used to filter search results.
 * @namespace domain.filters
 */


const context = require.context('./', true, /^(.(?!index\.|Spec\.))*js$/)

context.keys().forEach(key => {
  context(key).default(ngDomainModule)
})

export default ngDomainModule.name
