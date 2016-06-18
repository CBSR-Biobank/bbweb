/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([
  'angular',
  'angularMocks',
  'lodash',
  'biobankApp'
], function(angular, mocks, _) {
  'use strict';

  xdescribe('SpecimenGroup', function() {

    var httpBackend,
        funutils,
        SpecimenGroup,
        factory;

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function($httpBackend,
                               _funutils_,
                               _SpecimenGroup_,
                               factory,
                               serverReplyMixin,
                               extendedDomainEntities) {
      _.extend(this, serverReplyMixin);
      httpBackend   = $httpBackend;
      funutils      = _funutils_;
      SpecimenGroup = _SpecimenGroup_;
      factory  = factory;
    }));

    function uri(/* studyId, specimenGroupId, version */) {
      var args = _.toArray(arguments), studyId, specimenGroupId, version, result;

      if (arguments.length < 1) {
        throw new Error('study id not specified');
      }

      studyId = args.shift();
      result = '/studies/' + studyId + '/sgroups';

      if (args.length > 0) {
        specimenGroupId = args.shift();
        result += '/' + specimenGroupId;
      }
      if (args.length > 0) {
        version = args.shift();
        result += '/' + version;
      }
      return result;
    }

    function createEntities(options) {
      var study = factory.study(),
          serverSg = factory.specimenGroup(study),
          specimenGroup;

      options = options || {};

      if (options.noSgId) {
        specimenGroup = new SpecimenGroup(_.omit(serverSg, 'id'));
      } else {
        specimenGroup = new SpecimenGroup(serverSg);
      }
      return {
        study:         study,
        serverSg:      serverSg,
        specimenGroup: specimenGroup
      };
    }

    it('constructor with no parameters has default values', function() {
      var specimenGroup = new SpecimenGroup();

      expect(specimenGroup.isNew()).toBe(true);
      expect(specimenGroup.studyId).toBe(null);
      expect(specimenGroup.name).toBe('');
      expect(specimenGroup.description).toBe(null);
      expect(specimenGroup.units).toBe('');
      expect(specimenGroup.anatomicalSourceType).toBe('');
      expect(specimenGroup.preservationType).toBe('');
      expect(specimenGroup.preservationTemperatureType).toBe('');
      expect(specimenGroup.specimenType).toBe('');
    });

    it('fails when creating from a non object', function() {
      expect(SpecimenGroup.create(1))
        .toEqual(new Error('invalid object from server: must be a map, has the correct keys'));
    });

    it('has valid values when creating from server response', function() {
      var entities = createEntities();
      entities.specimenGroup = SpecimenGroup.create(entities.serverSg);
      entities.specimenGroup.compareToJsonEntity(entities.serverSg);
    });

    it('can retrieve a specimen group', function(done) {
      var entities = createEntities();
      httpBackend.whenGET(uri(entities.study.id) + '?sgId=' + entities.serverSg.id)
        .respond(this.reply(entities.serverSg));

      SpecimenGroup.get(entities.study.id, entities.serverSg.id).then(function(sg) {
        sg.compareToJsonEntity(entities.serverSg);
        done();
      });
      httpBackend.flush();
    });

    it('can list specimen groups', function(done) {
      var entities = createEntities();
      httpBackend.whenGET(uri(entities.study.id)).respond(this.reply([ entities.serverSg ]));
      SpecimenGroup.list(entities.study.id).then(function(list) {
        _.each(list, function (sg) {
          sg.compareToJsonEntity(entities.serverSg);
        });
        done();
      });
      httpBackend.flush();
    });

    it('can add a specimen group', function() {
      var entities = createEntities({ noSgId: true }),
          cmd = specimenGroupToAddCommand(entities.specimenGroup);

      httpBackend.expectPOST(uri(entities.study.id), cmd)
        .respond(this.reply(entities.serverSg));

      entities.specimenGroup.addOrUpdate().then(function(sg) {
        sg.compareToJsonEntity(entities.serverSg);
      });
      httpBackend.flush();
    });

    it('can update a specimen group', function() {
      var entities = createEntities();

      var cmd = specimenGroupToUpdateCommand(entities.specimenGroup);
      httpBackend.expectPUT(uri(entities.study.id, entities.specimenGroup.id), cmd)
        .respond(this.reply(entities.serverSg));

      entities.specimenGroup.addOrUpdate().then(function(sg) {
        sg.compareToJsonEntity(entities.serverSg);
      });
      httpBackend.flush();
    });

    it('should remove a specimen group', function() {
      var entities = createEntities();

      httpBackend.expectDELETE(uri(entities.study.id, entities.specimenGroup.id, entities.specimenGroup.version))
        .respond(this.reply(true));

      entities.specimenGroup.remove();
      httpBackend.flush();
    });

    it('isNew should be true for a specimen group with no ID', function() {
      var entities = createEntities({ noSgId: true });
      expect(entities.specimenGroup.isNew()).toBe(true);
    });

    it('isNew should be false for a specimen group that has an ID', function() {
      var entities = createEntities();
      expect(entities.specimenGroup.isNew()).toBe(false);
    });

    it('study ID matches the study', function() {
      var entities = createEntities();
      expect(entities.specimenGroup.studyId).toBe(entities.study.id);
    });

    function specimenGroupToAddCommand(specimenGroup) {
      return _.extend(_.pick(specimenGroup,
                             'studyId',
                             'name',
                             'units',
                             'anatomicalSourceType',
                             'preservationType',
                             'preservationTemperatureType',
                             'specimenType'),
                      funutils.pickOptional(specimenGroup, 'description'));
    }

    function specimenGroupToUpdateCommand(specimenGroup) {
      return _.extend(specimenGroupToAddCommand(specimenGroup), {
        id: specimenGroup.id,
        expectedVersion: specimenGroup.version
      });
    }
  });

});
