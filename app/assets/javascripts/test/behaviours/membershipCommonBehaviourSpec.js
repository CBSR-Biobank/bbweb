/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var _ = require('lodash');

  function membershipCommonBehaviour(context) {

    describe('(shared)', function() {

      it('invoking constructor with no parameters has default values', function() {
        var membership = this.membershipFromConstructor();
        _.forEach([membership.studyData, membership.centreData], function (info) {
          expect(info.allEntities).toBeFalse();
          expect(info.entityData).toBeEmptyArray();
        });
      });

      describe('for creating', function() {

        it('can create from JSON', function() {
          var self = this,
              json = this.jsonMembershipWithEntities(),
              membership = this.membershipFromJson(json);
          _.forEach([ membership.studyData, membership.centreData ], function (info) {
            expect(info).toEqual(jasmine.any(self.EntitySet));
            expect(info.allEntities).toBeFalse();
            expect(info.entityData).toBeNonEmptyArray();
          });
        });

        it('fails when required fields are missing', function() {
          var self = this,
              json = this.jsonMembershipWithEntities();
          _.forEach(['id', 'version', 'timeAdded', 'studyData', 'centreData'], function (field) {
            expect(function () {
              var badJson = _.omit(json, [field]);
              self.membershipFromJson(badJson);
            }).toThrowError(/Missing required property/);
          });
        });

      });

      describe('for creating asynchronously', function() {

        it('can create from JSON', function() {
          var self = this,
              json = this.jsonMembershipWithEntities();

          this.membershipFromJsonAsync(json)
            .then(function (membership) {
              _.forEach([membership.studyData, membership.centreData], function (info) {
                expect(info).toEqual(jasmine.any(self.EntitySet));
                expect(info.allEntities).toBeFalse();
                expect(info.entityData).toBeNonEmptyArray();
              });
            })
            .catch(function (err) {
              fail('should never be called: ' + err.message);
            });
          this.$rootScope.$digest();
        });

        it('fails when required fields are missing', function() {
          var self = this,
              json = this.jsonMembershipWithEntities();

          _.forEach(['id', 'version', 'timeAdded', 'studyData', 'centreData'], function (field) {
            var badJson = _.omit(json, [field]);
            self.membershipFromJsonAsync(badJson)
              .then(function () {
                fail('should never be called');
              })
              .catch(function (err) {
                expect(err.message).toContain('Missing required property');
              });
          });
          this.$rootScope.$digest();
        });

      });

      describe('for studies', function() {
        var subContext = {};

        beforeEach(function () {
          subContext.fieldName              = 'studyData';
          subContext.jsonWithAllEntitesFunc = this.jsonMembershipWithAllStudies;
          subContext.jsonWithEntityFunc     = this.jsonMembershipWithStudy;
          subContext.isForAllFunc           = context.isForAllStudiesFunc;
          subContext.isMemberOfFunc         = context.isMemberOfStudyFunc;
        });
        membershipEntitiesSharedBehaviour(subContext);
      });

      describe('for centres', function() {
        var subContext = {};

        beforeEach(function () {
          subContext.fieldName              = 'centreData';
          subContext.jsonWithAllEntitesFunc = this.jsonMembershipWithAllCentres;
          subContext.jsonWithEntityFunc     = this.jsonMembershipWithCentre;
          subContext.isForAllFunc           = context.isForAllCentresFunc;
          subContext.isMemberOfFunc         = context.isMemberOfCentreFunc;
          });
        membershipEntitiesSharedBehaviour(subContext);
      });

    });

    function membershipEntitiesSharedBehaviour(context) {

      describe('(shared)', function () {

        beforeEach(function() {
          this.fixtures = function (jsonWithEntityFunc) {
            var id         = this.Factory.stringNext(),
                name       = this.Factory.stringNext(),
                json       = jsonWithEntityFunc.call(this, id, name),
                membership = this.membershipFromJson(json);
            return {
              id:         id,
              name:       name,
              json:       json,
              membership: membership
            };
          };
        });

        it('isForAll returns valid result', function() {
          var json = context.jsonWithAllEntitesFunc.call(this),
              membership = this.membershipFromJson(json);
          expect(context.isForAllFunc.call(membership)).toBeTrue();
        });

        it('returns true for a name in the set', function() {
          var f = this.fixtures(context.jsonWithEntityFunc);
          expect(context.isForAllFunc.call(f.membership)).toBeFalse();
          expect(context.isMemberOfFunc.call(f.membership, f.name)).toBeTrue();
        });

        it('returns false for a name not in the set', function() {
          var f = this.fixtures(context.jsonWithEntityFunc);
          expect(context.isMemberOfFunc.call(f.membership, this.Factory.stringNext())).toBeFalse();
        });

      });

    }

  }

  return membershipCommonBehaviour;

});
