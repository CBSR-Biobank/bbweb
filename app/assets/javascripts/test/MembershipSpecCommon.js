/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _      = require('lodash'),
      moment = require('moment'),
      faker  = require('faker');

  MebershipSpecCommonFactory.$inject = ['EntityTestSuite', 'Factory'];

  function MebershipSpecCommonFactory(EntityTestSuite, factory) {

    function MebershipSpecCommon() {
      EntityTestSuite.call(this);
    }

    MebershipSpecCommon.prototype = Object.create(EntityTestSuite.prototype);
    MebershipSpecCommon.prototype.constructor = MebershipSpecCommon;

    MebershipSpecCommon.prototype.jsonObj = function () {
      return factory.membershipBase();
    };

    MebershipSpecCommon.prototype.jsonObjWithEntities = function (studyEntityData, centreEntityData) {
      return _.extend(factory.membershipBase(),
                      {
                        studyData:  { allEntities: false, entityData: studyEntityData },
                        centreData: { allEntities: false, entityData: centreEntityData }
                      });
    };

    MebershipSpecCommon.prototype.jsonEntityData = function () {
      return factory.membershipEntityData();
    };

    MebershipSpecCommon.prototype.membershipFromConstructor = function () {
      return new this.MembershipBase();
    };

    MebershipSpecCommon.prototype.membershipFromJson = function (json) {
      return this.MembershipBase.create(json);
    };

    MebershipSpecCommon.prototype.membershipFromJsonAsync = function (json) {
      return this.MembershipBase.asyncCreate(json);
    };

    MebershipSpecCommon.prototype.jsonMembershipWithStudy = function (id, name) {
      var json = factory.membershipBase();
      json.studyData.entityData = [{ id: id, name: name }];
      return json;
    };

    MebershipSpecCommon.prototype.jsonMembershipWithAllStudies = function () {
      var json = factory.membershipBase();
      json.studyData.allEntities = true;
      return json;
    };

    MebershipSpecCommon.prototype.jsonMembershipWithCentre = function (id, name) {
      var json = factory.membershipBase();
      json.centreData.entityData = [{ id: id, name: name}];
      return json;
    };

    MebershipSpecCommon.prototype.jsonMembershipWithAllCentres = function () {
      var json = factory.membershipBase();
      json.centreData.allEntities = true;
      return json;
    };

    MebershipSpecCommon.prototype.jsonMembershipWithEntities = function () {
      var entityData = [ factory.membershipEntityData() ];
      return this.jsonObjWithEntities(entityData, entityData);
    };

    return MebershipSpecCommon;
  }

  return MebershipSpecCommonFactory;

});
