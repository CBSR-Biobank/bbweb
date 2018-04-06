/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { EntityTestSuiteMixin } from 'test/mixins/EntityTestSuiteMixin';
import _ from 'lodash';

/**
 * This is a mixin that can be added UserContext object of a Jasmine test suite.
 *
 * @exports test.mixins.MembershipTestSuiteMixin
 */
let MembershipTestSuiteMixin = {

  /**
   * Used to inject AngularJS dependencies into the test suite.
   *
   * Also injects dependencies required by this mixin.
   *
   * @param {...string} dependencies - the AngularJS dependencies to inject.
   *
   * @return {undefined}
   */
  injectDependencies: function (...dependencies) {
    const allDependencies = dependencies.concat([ 'Factory' ]);
    EntityTestSuiteMixin.injectDependencies.call(this, ...allDependencies);
  },

  jsonObj: function () {
    return this.Factory.membershipBase();
  },

  jsonObjWithEntities: function (studyEntityData, centreEntityData) {
    return Object.assign(this.Factory.membershipBase(),
                    {
                      studyData:  { allEntities: false, entityData: studyEntityData },
                      centreData: { allEntities: false, entityData: centreEntityData }
                    });
  },

  jsonEntityData: function () {
    return this.Factory.entityInfo();
  },

  membershipFromConstructor: function () {
    return new this.MembershipBase();
  },

  membershipFromJson: function (json) {
    return this.MembershipBase.create(json);
  },

  membershipFromJsonAsync: function (json) {
    return this.MembershipBase.asyncCreate(json);
  },

  jsonMembershipWithStudy: function (id, name) {
    var json = this.Factory.membershipBase();
    json.studyData.entityData = [{ id: id, name: name }];
    return json;
  },

  jsonMembershipWithAllStudies: function () {
    var json = this.Factory.membershipBase();
    json.studyData.allEntities = true;
    return json;
  },

  jsonMembershipWithCentre: function (id, name) {
    var json = this.Factory.membershipBase();
    json.centreData.entityData = [{ id: id, name: name}];
    return json;
  },

  jsonMembershipWithAllCentres: function () {
    var json = this.Factory.membershipBase();
    json.centreData.allEntities = true;
    return json;
  },

  jsonMembershipWithEntities: function () {
    var entityData = [ this.Factory.entityInfo() ];
    return this.jsonObjWithEntities(entityData, entityData);
  },

  entityInfoFrom: function (entity) {
    return _.pick(entity, 'id', 'name');
  },

  entityNameFrom: function (entity) {
    return _.pick(entity, 'id', 'name', 'state');
  }

}

MembershipTestSuiteMixin = Object.assign({},
                                        EntityTestSuiteMixin,
                                        MembershipTestSuiteMixin);

export { MembershipTestSuiteMixin };
export default () => {};
