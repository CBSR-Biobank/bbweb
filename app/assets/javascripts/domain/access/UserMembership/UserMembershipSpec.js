/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
/* global angular */

import { MembershipTestSuiteMixin } from 'test/mixins/MembershipTestSuiteMixin';
import { ServerReplyMixin } from 'test/mixins/ServerReplyMixin';
import membershipCommonBehaviour from 'test/behaviours/membershipCommonBehaviourSpec';
import ngModule from '../../index'

describe('UserMembership', function() {

  const SuiteMixin = {

    jsonObj: function () {
      return this.Factory.userMembership();
    },

    membershipFromConstructor: function () {
      return new this.UserMembership();
    },

    membershipFromJson: function (json) {
      return this.UserMembership.create(json);
    },

    membershipFromJsonAsync: function (json) {
      return this.UserMembership.asyncCreate(json);
    },

    jsonMembershipWithAllStudies: function () {
      var json = this.Factory.userMembership();
      json.studyData.allEntities = true;
      return json;
    },

    jsonMembershipWithStudy: function (id, name) {
      var json = this.Factory.userMembership();
      json.studyData.entityData = [ Object.assign(this.Factory.entityInfo(), { id: id, name: name}) ];
      return json;
    },

    jsonMembershipWithAllCentres: function () {
      var json = this.Factory.userMembership();
      json.centreData.allEntities = true;
      return json;
    },

    jsonMembershipWithCentre: function (id, name) {
      var json = this.Factory.userMembership();
      json.centreData.entityData = [ Object.assign(this.Factory.entityInfo(), { id: id, name: name}) ];
      return json;
    },

    jsonMembershipWithEntities: function () {
      var entityData = [ this.jsonEntityData() ];
      return this.jsonObjWithEntities(entityData, entityData);
    },

    fixtures: function (options) {
      var jsonMembership = this.Factory.userMembership(options),
          membership     = this.UserMembership.create(jsonMembership);
      return {
        jsonMembership: jsonMembership,
        membership:     membership
      };
    }
  }

  beforeEach(() => {
    angular.mock.module(ngModule, 'biobank.test');
    angular.mock.inject(function() {
      Object.assign(this, MembershipTestSuiteMixin, SuiteMixin, ServerReplyMixin);

      this.injectDependencies('$rootScope',
                              'UserMembership',
                              'EntitySet',
                              'Factory',
                              'TestUtils');

      this.TestUtils.addCustomMatchers();
    });
  });

  describe('shared behaviour', function() {
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
