/*
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import AdminCommonModule from '../common'
import BaseModule        from '../../../base'
import DomainModule      from '../../../domain'
import StudiesModule     from '../../../studies'
import UsersModule       from '../../../users'
import angular           from 'angular';

/**
 * AngularJS components for {@link domain.studies.Study Study} administration.
 * @namespace admin.studies
 */

/**
 * AngularJS components for {@link domain.studies.Study Study} administration.
 * @namespace admin.studies.components
 */

/**
 * AngularJS services for {@link domain.studies.Study Study} administration.
 * @namespace admin.studies.services
 */

/**
 * A Webpack module for {@link domain.studies.Study Study} Administration.
 *
 * @memberOf admin.studies
 * @type {AngularJS_Module}
 */
const ngAdminStudiesModule = angular.module('biobank.admin.studies', [
  AdminCommonModule,
  BaseModule,
  DomainModule,
  StudiesModule,
  UsersModule
])

const context = require.context('./', true, /^(?:.(?![\\\/]modules[\\\/]|index\.js|Spec\.js))*\.js$/)

//console.log(context.keys())

context.keys().forEach(key => {
  context(key).default(ngAdminStudiesModule)
})

export default ngAdminStudiesModule.name
