/*
 * Centres Admin module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import CentresModule from '../../../centres';
import StudiesModule from '../../../studies';
import UsersModule from '../../../users';
import angular from 'angular';

/**
 * AngularJS components for {@link domain.centres.Centre Centre} administration.
 * @namespace admin.centres
 */

/**
 * AngularJS components for {@link domain.centres.Centre Centre} administration.
 * @namespace admin.centres.components
 */

/**
 * A Webpack module for {@link domain.centres.Centre Centre} Administration.
 *
 * @memberOf admin.centres
 * @type {AngularJS_Module}
 */
const ngAdminCentresModule = angular.module(
  'biobank.admin.centres',
  [
    CentresModule,
    StudiesModule,
    UsersModule
  ])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngAdminCentresModule)
})

export default ngAdminCentresModule.name
