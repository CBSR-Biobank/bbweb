/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'underscore',
  '../enumSharedSpec',
  'biobankApp'
], function(angular, mocks, _, enumSharedSpec) {
  'use strict';

  describe('UserStatus', function() {

    var context = {};

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function (UserStatus) {
      context.enumerationClass = UserStatus;
      context.valueMap = [
        [ 'ActiveUser',     'ACTIVE' ],
        [ 'RegisteredUser', 'REGISTERED' ],
        [ 'LockedUser',     'LOCKED' ]
      ];
    }));

    enumSharedSpec(context);
  });

});
