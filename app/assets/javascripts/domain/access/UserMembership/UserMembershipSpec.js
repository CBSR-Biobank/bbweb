/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import _ from 'lodash';
import membershipCommonBehaviour from '../../../test/behaviours/membershipCommonBehaviourSpec';

describe('UserMembership', function() {

  function SuiteMixin(MembershipSpecCommon, ServerReplyMixin) {

    return _.extend({},
                    MembershipSpecCommon,
                    ServerReplyMixin,
                    {
                      jsonObj,
                      membershipFromConstructor,
                      membershipFromJson,
                      membershipFromJsonAsync,
                      jsonMembershipWithAllStudies,
                      jsonMembershipWithStudy,
                      jsonMembershipWithAllCentres,
                      jsonMembershipWithCentre,
                      jsonMembershipWithEntities,
                      fixtures
                    });

    function jsonObj() {
      return this.Factory.userMembership();
    }

    function membershipFromConstructor() {
      return new this.UserMembership();
    }

    function membershipFromJson(json) {
      return this.UserMembership.create(json);
    }

    function membershipFromJsonAsync(json) {
      return this.UserMembership.asyncCreate(json);
    }

    function jsonMembershipWithAllStudies() {
      var json = this.Factory.userMembership();
      json.studyData.allEntities = true;
      return json;
    }

    function jsonMembershipWithStudy(id, name) {
      var json = this.Factory.userMembership();
      json.studyData.entityData = [{ id: id, name: name}];
      return json;
    }

    function jsonMembershipWithAllCentres() {
      var json = this.Factory.userMembership();
      json.centreData.allEntities = true;
      return json;
    }

    function jsonMembershipWithCentre(id, name) {
      var json = this.Factory.userMembership();
      json.centreData.entityData = [{ id: id, name: name}];
      return json;
    }

    function jsonMembershipWithEntities() {
      var entityData = [ this.jsonEntityData() ];
      return this.jsonObjWithEntities(entityData, entityData);
    }

    function fixtures(options) {
      var jsonMembership = this.Factory.userMembership(options),
          membership     = this.UserMembership.create(jsonMembership);
      return {
        jsonMembership: jsonMembership,
        membership:     membership
      };
    }
  }

  beforeEach(() => {
    angular.mock.module('biobankApp', 'biobank.test');
    angular.mock.inject(function(MembershipSpecCommon, ServerReplyMixin) {
      _.extend(this, SuiteMixin(MembershipSpecCommon, ServerReplyMixin));

      this.injectDependencies('$rootScope',
                              'UserMembership',
                              'EntitySet',
                              'Factory',
                              'TestUtils');

      this.TestUtils.addCustomMatchers();
    });
  });

  describe('base class shared behaviour', function() {
    var context = {};

    beforeEach(function() {
      context.isForAllStudiesFunc  = this.UserMembership.prototype.isForAllStudies;
      context.isMemberOfStudyFunc  = this.UserMembership.prototype.isMemberOfStudy;
      context.isForAllCentresFunc  = this.UserMembership.prototype.isForAllCentres;
      context.isMemberOfCentreFunc = this.UserMembership.prototype.isMemberOfCentre;
    });

    membershipCommonBehaviour(context);
  });

});
