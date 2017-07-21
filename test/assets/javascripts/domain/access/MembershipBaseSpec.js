/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks  = require('angularMocks'),
      _      = require('lodash'),
      membershipCommonBehaviour = require('../../test/membershipCommonBehaviourSpec');

  describe('MembershipBase', function() {

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(MebershipSpecCommon, testDomainEntities) {
      _.extend(this, MebershipSpecCommon.prototype);

      this.injectDependencies('$rootScope',
                              'MembershipBase',
                              'EntitySet',
                              'factory',
                              'testUtils');

      this.testUtils.addCustomMatchers();
      testDomainEntities.extend();
    }));


    describe('base class shared behaviour', function() {
      var context = {};

      beforeEach(function() {
        context.isForAllStudiesFunc  = this.MembershipBase.prototype.isForAllStudies;
        context.isMemberOfStudyFunc  = this.MembershipBase.prototype.isMemberOfStudy;
        context.isForAllCentresFunc  = this.MembershipBase.prototype.isForAllCentres;
        context.isMemberOfCentreFunc = this.MembershipBase.prototype.isMemberOfCentre;
      });

      membershipCommonBehaviour(context);
    });

  });

});
