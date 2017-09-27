/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import membershipCommonBehaviour from '../../../test/membershipCommonBehaviourSpec';

describe('MembershipBase', function() {

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(MebershipSpecCommon, testDomainEntities) {
      _.extend(this, MebershipSpecCommon.prototype);

      this.injectDependencies('$rootScope',
                              'MembershipBase',
                              'EntitySet',
                              'factory',
                              'testUtils');

      this.testUtils.addCustomMatchers();
      testDomainEntities.extend();
    });
  });

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
