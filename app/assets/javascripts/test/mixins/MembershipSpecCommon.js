/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import _ from 'lodash';

/* @ngInject */
function MembershipSpecCommon(EntityTestSuiteMixin, Factory) {

  return _.extend({},
                  EntityTestSuiteMixin,
                  {
                    jsonObj:                      jsonObj,
                    jsonObjWithEntities:          jsonObjWithEntities,
                    jsonEntityData:               jsonEntityData,
                    membershipFromConstructor:    membershipFromConstructor,
                    membershipFromJson:           membershipFromJson,
                    membershipFromJsonAsync:      membershipFromJsonAsync,
                    jsonMembershipWithStudy:      jsonMembershipWithStudy,
                    jsonMembershipWithAllStudies: jsonMembershipWithAllStudies,
                    jsonMembershipWithCentre:     jsonMembershipWithCentre,
                    jsonMembershipWithAllCentres: jsonMembershipWithAllCentres,
                    jsonMembershipWithEntities:   jsonMembershipWithEntities
                  });

  function jsonObj() {
    return Factory.membershipBase();
  }

  function jsonObjWithEntities(studyEntityData, centreEntityData) {
    return _.extend(Factory.membershipBase(),
                    {
                      studyData:  { allEntities: false, entityData: studyEntityData },
                      centreData: { allEntities: false, entityData: centreEntityData }
                    });
  }

  function jsonEntityData() {
    return Factory.membershipEntityData();
  }

  function membershipFromConstructor() {
    return new this.MembershipBase();
  }

  function membershipFromJson(json) {
    return this.MembershipBase.create(json);
  }

  function membershipFromJsonAsync(json) {
    return this.MembershipBase.asyncCreate(json);
  }

  function jsonMembershipWithStudy(id, name) {
    var json = Factory.membershipBase();
    json.studyData.entityData = [{ id: id, name: name }];
    return json;
  }

  function jsonMembershipWithAllStudies() {
    var json = Factory.membershipBase();
    json.studyData.allEntities = true;
    return json;
  }

  function jsonMembershipWithCentre(id, name) {
    var json = Factory.membershipBase();
    json.centreData.entityData = [{ id: id, name: name}];
    return json;
  }

  function jsonMembershipWithAllCentres() {
    var json = Factory.membershipBase();
    json.centreData.allEntities = true;
    return json;
  }

  function jsonMembershipWithEntities() {
    var entityData = [ Factory.membershipEntityData() ];
    return this.jsonObjWithEntities(entityData, entityData);
  }

}

export default ngModule => ngModule.service('MembershipSpecCommon', MembershipSpecCommon)
