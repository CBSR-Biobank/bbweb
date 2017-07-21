/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks  = require('angularMocks'),
      _      = require('lodash'),
      sprintf = require('sprintf-js').sprintf,
      membershipCommonBehaviour = require('../../test/membershipCommonBehaviourSpec');

  describe('Membership', function() {

    function SuiteMixinFactory(MebershipSpecCommon) {

      function SuiteMixin() {
        MebershipSpecCommon.call(this);
      }

      SuiteMixin.prototype = Object.create(MebershipSpecCommon.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.jsonObj = function () {
        return this.factory.membership();
      };

      SuiteMixin.prototype.jsonObjWithEntities = function (studyEntityData, centreEntityData) {
        return _.extend(
          MebershipSpecCommon.prototype.jsonObjWithEntities.call(this, studyEntityData, centreEntityData),
          { userData: [] });
      };

      SuiteMixin.prototype.membershipFromConstructor = function () {
        return new this.Membership();
      };

      SuiteMixin.prototype.membershipFromJson = function (json) {
        return this.Membership.create(json);
      };

      SuiteMixin.prototype.membershipFromJsonAsync = function (json) {
        return this.Membership.asyncCreate(json);
      };

      SuiteMixin.prototype.jsonMembershipWithAllStudies = function () {
        var json = this.factory.membership();
        json.studyData.allEntities = true;
        return json;
      };

      SuiteMixin.prototype.jsonMembershipWithStudy = function (id, name) {
        var json = this.factory.membership();
        json.studyData.entityData = [{ id: id, name: name}];
        return this.Membership.create(json);
      };

      SuiteMixin.prototype.jsonMembershipWithAllCentres = function () {
        var json = this.factory.membership();
        json.centreData.allEntities = true;
        return json;
      };

      SuiteMixin.prototype.jsonMembershipWithCentre = function (id, name) {
        var json = this.factory.membership();
        json.centreData.entityData = [{ id: id, name: name}];
        return this.Membership.create(json);
      };

      SuiteMixin.prototype.jsonMembershipWithEntities = function () {
        var entityData = [ this.jsonEntityData() ],
            json = this.jsonObjWithEntities(entityData, entityData);
        return this.Membership.create(json);
      };

      SuiteMixin.prototype.fixtures = function (options) {
        var jsonMembership = this.factory.membership(options),
            membership     = this.Membership.create(jsonMembership);
        return {
          jsonMembership: jsonMembership,
          membership:     membership
        };
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(MebershipSpecCommon, ServerReplyMixin, testDomainEntities) {
      var self = this;

      _.extend(this, new SuiteMixinFactory(MebershipSpecCommon).prototype, ServerReplyMixin.prototype);

      this.injectDependencies('$rootScope',
                              '$httpBackend',
                              '$httpParamSerializer',
                              'Membership',
                              'EntitySet',
                              'EntityInfo',
                              'factory',
                              'testUtils');

      this.testUtils.addCustomMatchers();
      testDomainEntities.extend();

      // used by promise tests
      this.expectMembership = function(entity) {
        expect(entity).toEqual(jasmine.any(self.Membership));
      };
    }));

    afterEach(function() {
      this.$httpBackend.verifyNoOutstandingExpectation();
      this.$httpBackend.verifyNoOutstandingRequest();
    });

    describe('base class shared behaviour', function() {
      var context = {};
      beforeEach(function() {
        context.isForAllStudiesFunc  = this.Membership.prototype.isForAllStudies;
        context.isMemberOfStudyFunc  = this.Membership.prototype.isMemberOfStudy;
        context.isForAllCentresFunc  = this.Membership.prototype.isForAllCentres;
        context.isMemberOfCentreFunc = this.Membership.prototype.isMemberOfCentre;
      });
      membershipCommonBehaviour(context);
    });

    it('can be constructed with user data', function() {
      var json = this.factory.membership({
        userData: [{
          id:   this.factory.stringNext(),
          name: this.factory.stringNext()
        }]
      }),
          membership = new this.Membership(json);
      expect(membership).toHaveArrayOfSize('userData', json.userData.length);
      expect(membership.userData[0]).toEqual(jasmine.any(this.EntityInfo));
    });

    describe('when getting a single membership', function() {

      it('can retrieve a single membership', function() {
        var id = this.factory.stringNext(),
            json = this.factory.membership();
        this.$httpBackend.whenGET(uri(id)).respond(this.reply(json));
        this.Membership.get(id).then(this.expectMembership).catch(failTest);
        this.$httpBackend.flush();
      });

      it('fails when getting a membership and it has a bad format', function() {
        var self = this;

        this.Membership.SCHEMA.required.forEach(function (property) {
          var json = self.factory.membership(),
              badJson = _.omit(json, property);
          self.$httpBackend.whenGET(uri(json.id)).respond(self.reply(badJson));
          self.Membership.get(json.id).then(shouldNotFail).catch(shouldFail);
          self.$httpBackend.flush();
        });

        function shouldNotFail() {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error.message).toContain('Missing required property');
        }
      });

    });

    describe('when listing memberships', function() {

      it('can retrieve studies', function() {
        var self = this,
            memberships = [ this.factory.membership() ],
            reply = this.factory.pagedResult(memberships);

        self.$httpBackend.whenGET(uri()).respond(this.reply(reply));
        self.Membership.list().then(testMembership).catch(failTest);
        self.$httpBackend.flush();

        function testMembership(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(1);
          expect(pagedResult.items[0]).toEqual(jasmine.any(self.Membership));
        }
      });

      it('can use options', function() {
        var self = this,
            optionList = [
              { filter: 'name::test' },
              { page: 2 },
              { limit: 10 }
            ],
            memberships = [ this.factory.membership() ],
            reply = this.factory.pagedResult(memberships);

        optionList.forEach(function (options) {
          var url = sprintf('%s?%s', uri(), self.$httpParamSerializer(options, true));

          self.$httpBackend.whenGET(url).respond(self.reply(reply));
          self.Membership.list(options).then(testMembership).catch(failTest);
          self.$httpBackend.flush();

          function testMembership(pagedResult) {
            expect(pagedResult.items).toBeArrayOfSize(memberships.length);
            _.each(pagedResult.items, function (study) {
              expect(study).toEqual(jasmine.any(self.Membership));
            });
          }
        });
      });

      it('listing omits empty options', function() {
        var self = this,
            options = { filter: ''},
            memberships = [ this.factory.membership() ],
            reply = this.factory.pagedResult(memberships);

        this.$httpBackend.whenGET(uri()).respond(this.reply(reply));
        this.Membership.list(options).then(testMembership).catch(failTest);
        this.$httpBackend.flush();

        function testMembership(pagedResult) {
          expect(pagedResult.items).toBeArrayOfSize(memberships.length);
          _.each(pagedResult.items, function (study) {
            expect(study).toEqual(jasmine.any(self.Membership));
          });
        }
      });

      it('fails when an invalid membership is returned', function() {
        var json = [ _.omit(this.factory.membership(), 'id') ],
            reply = this.factory.pagedResult(json);

        this.$httpBackend.whenGET(uri()).respond(this.reply(reply));
        this.Membership.list().then(listFail).catch(shouldFail);
        this.$httpBackend.flush();

        function listFail(reply) {
          fail('function should not be called');
        }

        function shouldFail(error) {
          expect(error).toStartWith('invalid memberships from server');
        }
      });

    });

    it('can add a membership', function() {
      var options =
          {
            userData: [ this.factory.membershipEntityData() ],
            studyData: {
              allEntities: false,
              entityData: [ this.factory.membershipEntityData() ]
            },
            centreData: {
              allEntities: false,
              entityData: [ this.factory.membershipEntityData() ]
            }
          },
          f       = this.fixtures(options),
          reqJson = _.pick(f.jsonMembership, 'name', 'description');

      reqJson.userIds    = f.jsonMembership.userData.map(getEntityDataIds);
      reqJson.allStudies = f.jsonMembership.studyData.allEntities;
      reqJson.studyIds   = f.jsonMembership.studyData.entityData.map(getEntityDataIds);
      reqJson.allCentres = f.jsonMembership.centreData.allEntities;
      reqJson.centreIds  = f.jsonMembership.centreData.entityData.map(getEntityDataIds);

      this.$httpBackend.expectPOST(uri(), reqJson).respond(this.reply(f.jsonMembership));
      f.membership.add().then(this.expectStudy).catch(failTest);
      this.$httpBackend.flush();

      function getEntityDataIds(entityInfo) {
        return entityInfo.id;
      }

    });

    describe('when removing a membership', function() {

      it('should remove a membership', function() {
        var membership = this.Membership.create(this.factory.membership()),
            url = sprintf('%s%s/%d',
                          uri(),
                          membership.id,
                          membership.version);

        this.$httpBackend.expectDELETE(url).respond(this.reply(true));
        membership.remove();
        this.$httpBackend.flush();
      });

      it('cannot remove a new membership', function() {
        var membership = new this.Membership();
        expect(function () {
          membership.remove();
        }).toThrowError(/membership has not been persisted/);
      });

    });

    it('can update the name', function() {
      var f    = this.fixtures(),
          name = this.factory.stringNext();

      this.updateEntity.call(this,
                             f.membership,
                             'updateName',
                             name,
                             updateUri('name', f.membership.id),
                             { name: name },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    it('can update the description', function() {
      var f           = this.fixtures(),
          description = this.factory.stringNext();

      this.updateEntity.call(this,
                             f.membership,
                             'updateDescription',
                             undefined,
                             updateUri('description', f.membership.id),
                             { },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);

      this.updateEntity.call(this,
                             f.membership,
                             'updateDescription',
                             description,
                             updateUri('description', f.membership.id),
                             { description: description },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    it('can add a user', function() {
      var f      = this.fixtures(),
          userId = this.factory.stringNext();

      this.updateEntity.call(this,
                             f.membership,
                             'addUser',
                             userId,
                             updateUri('user', f.membership.id),
                             { userId: userId },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    describe('when removing a user', function() {

      it('can remove a user', function() {
        var self    = this,
            f       = this.fixtures({ userData: [ this.factory.membershipEntityData() ] }),
            userId  = f.jsonMembership.userData[0].id,
            url     = sprintf('%s/%d/%s',
                              updateUri('user', f.membership.id),
                              f.membership.version,
                              userId);

        this.$httpBackend.expectDELETE(url).respond(this.reply(f.jsonMembership));
        f.membership.removeUser(userId)
          .then(function (reply) {
            expect(reply).toEqual(jasmine.any(self.Membership));
          })
          .catch(failTest);
        this.$httpBackend.flush();
      });

      it('cannot remove a user from a new membership', function() {
        var membership = new this.Membership();
        expect(function () {
          membership.removeUser();
        }).toThrowError(/membership has not been persisted/);
      });

    });

    it('can assign to be for all studies', function() {
      var f = this.fixtures();

      this.updateEntity.call(this,
                             f.membership,
                             'allStudies',
                             undefined,
                             updateUri('allStudies', f.membership.id),
                             {},
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    it('can add a study', function() {
      var f       = this.fixtures(),
          studyId = this.factory.stringNext();

      this.updateEntity.call(this,
                             f.membership,
                             'addStudy',
                             studyId,
                             updateUri('study', f.membership.id),
                             { studyId: studyId },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    it('can assign to be for all centres', function() {
      var f = this.fixtures();

      this.updateEntity.call(this,
                             f.membership,
                             'allCentres',
                             undefined,
                             updateUri('allCentres', f.membership.id),
                             {},
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    it('can add a centre', function() {
      var f        = this.fixtures(),
          centreId = this.factory.stringNext();

      this.updateEntity.call(this,
                             f.membership,
                             'addCentre',
                             centreId,
                             updateUri('centre', f.membership.id),
                             { centreId: centreId },
                             f.jsonMembership,
                             this.expectMembership,
                             failTest);
    });

    describe('can remove a study', function() {
      var context = {};
      beforeEach(function () {
        context.entityName = 'study';
        context.jsonWithEntityFunc = this.jsonMembershipWithStudy;
        context.removeEntityFunc = this.Membership.prototype.removeStudy;
      });
      removeEntitySharedBehaviour(context);
    });

    describe('can remove a centre', function() {
      var context = {};
      beforeEach(function () {
        var self = this;
        context.entityName = 'centre';
        context.jsonWithEntityFunc = self.jsonMembershipWithCentre;
        context.removeEntityFunc = this.Membership.prototype.removeCentre;
      });
      removeEntitySharedBehaviour(context);
    });

    function removeEntitySharedBehaviour(context) {

      describe('(shared) removes an entity', function () {

        it('makes the correct REST API call', function() {
          var self       = this,
              entityId   = this.factory.stringNext(),
              fieldName  = context.entityName + 'Data',
              json       = context.jsonWithEntityFunc.call(this, entityId, this.factory.stringNext()),
              membership = this.membershipFromJson(json),
              url        = sprintf('%s/%d/%s',
                                   updateUri(context.entityName, membership.id),
                                   membership.version,
                                   entityId);

          expect(membership[fieldName].allEntities).toBeFalse();
          expect(membership[fieldName].entityData).toBeNonEmptyArray();

          this.$httpBackend.expectDELETE(url).respond(this.reply(json));

          context.removeEntityFunc.call(membership, entityId)
            .then(function (reply) {
              expect(reply).toEqual(jasmine.any(self.Membership));
            })
            .catch(failTest);
          this.$httpBackend.flush();
        });
      });

      it('throws a domain error if membership has not been persited yet', function() {
        var self       = this,
            membership = this.membershipFromConstructor();

        expect(function () {
          context.removeEntityFunc.call(membership, self.factory.stringNext());
        }).toThrowError(/membership has not been persisted/);
      });

    }

    function uri(membershipId) {
      var result = '/access/memberships/';
      if (arguments.length > 0) {
        result += membershipId;
      }
      return result;
    }

    function updateUri(/* path, membershipId */) {
      var result = uri(),
          args = _.toArray(arguments),
          path,
          membershipId;

      if (args.length > 0) {
        path = args.shift();
        result += path;
      }

      if (args.length > 0) {
        membershipId = args.shift();
        result += '/' + membershipId;
      }
      return result;
    }

    // used by promise tests
    function failTest(error) {
      expect(error).toBeUndefined();
    }

  });

});
