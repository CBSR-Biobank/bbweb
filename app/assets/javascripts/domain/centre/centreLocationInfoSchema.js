/**
 * Constant related to {@link domain.centres.Centre Centres}.
 *
 * @namespace domain.centres.centreLocationInfo
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 * @typedef domain.centres.centreLocationInfo.CentreLocationInfo
 *
 * @type object
 * @memberOf domain.centres.centreLocationInfo
 *
 * @property {string} centreId - the ID that identifies the centre.
 *
 * @property {string} locationId - the ID that identifies the location.
 *
 * @property {string} name - the centre's name concatenated with the location name.
 */
const centreLocationInfoSchema = {
  'id': 'CentreLocationInfo',
  'type': 'object',
  'properties': {
    'centreId':   { 'type': 'string' },
    'locationId': { 'type': 'string' },
    'name':       { 'type': 'string' }
  },
  'required': [
    'centreId',
    'locationId',
    'name',
  ]
};

export default ngModule => ngModule.constant('centreLocationInfoSchema', centreLocationInfoSchema)
